import {Component, inject, Input, OnInit, signal} from '@angular/core';
import {FormBuilder, FormGroup, ReactiveFormsModule, Validators} from '@angular/forms';
import {LucideAngularModule} from 'lucide-angular';
import {FormSection} from '../../../../shared/form-section/form-section';
import {InputField} from '../../../../shared/input-field/input-field';
import {Select} from '../../../../shared/select/select';
import {Button} from '../../../../shared/button/button';
import {COUNTRIES} from '../../../../core/constants/countries';
import {IUser} from '../../../../core/models/i-user';
import {UserService} from '../../../../core/service/user-service';
import {ToastService} from '../../../../core/service/toast-service';
import {UserAvatar} from '../../../../shared/user-avatar/user-avatar';
import {finalize} from 'rxjs';
import {FileService} from '../../../../core/service/file-service';

@Component({
  selector: 'app-profile-settings',
  imports: [
    LucideAngularModule,
    ReactiveFormsModule,
    FormSection,
    InputField,
    Select,
    Button,
    UserAvatar
  ],
  templateUrl: './profile-settings.html',
  styleUrl: './profile-settings.css'
})
export class ProfileSettings implements OnInit {
  private fb = inject(FormBuilder);
  private toastService = inject(ToastService);
  private userService = inject(UserService);
  private fileService = inject(FileService);

  loading = signal(false);

  user: IUser | null = null;
  selectedImage: File | null = null;
  selectedImagePreview: string | null = null;

  readonly countries = COUNTRIES.map(country => ({
    value: country.code,
    label: country.name,
    icon: country.flag
  }));

  form!: FormGroup;

  ngOnInit(): void {
    this.user = this.userService.getUser();
    this.initForm();
  }

  initForm(): void {
    this.form = this.fb.group({
      nickName: [this.user?.nickName || '', [Validators.required, Validators.minLength(3)]],
      bio: [this.user?.bio || ''],
      country: ['PL', [Validators.required]],
    });
  }

  getControl(name: string) {
    return this.form.get(name) as any;
  }


  onSubmit() {
    if (this.form.invalid) return;

    this.loading.set(true);

    const formValue = this.form.getRawValue();

    this.userService.updateProfile(formValue, this.selectedImage)
      .pipe(
        finalize(() => {
          this.loading.set(false);
          this.selectedImage = null;
        })
      )
      .subscribe({
        next: () => {
          this.toastService.show('Profil został zaktualizowany', 'success');
        },
        error: (err) => {
          if (err.error?.code === 'NICKNAME_ALREADY_EXISTS') {
            this.form.get('nickName')?.setErrors({ alreadyExists: true });
            this.toastService.show('Ta nazwa użytkownika jest już zajęta', 'error');
          } else {
            this.toastService.show('Wystąpił błąd podczas zapisywania', 'error');
          }
        }
      });
  }

  async onFileSelected(event: Event) {
    const result = await this.fileService.processSingleImage(event);
    if (result) {
      this.selectedImage = result.file;
      this.selectedImagePreview = result.preview;
    }
  }
}
