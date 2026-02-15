import {Component, inject, signal} from '@angular/core';
import {FormBuilder, FormGroup, ReactiveFormsModule} from '@angular/forms';
import {FormSection} from '../../../../shared/form-section/form-section';
import {Button} from '../../../../shared/button/button';
import {ToggleSwitch} from '../../../../shared/toggle-switch/toggle-switch';

@Component({
  selector: 'app-privacy-settings',
  imports: [
    ReactiveFormsModule,
    FormSection,
    Button,
    ToggleSwitch
  ],
  templateUrl: './privacy-settings.html',
  styleUrl: './privacy-settings.css'
})
export class PrivacySettings {
  private fb = inject(FormBuilder);
  loading = signal(false);

  form: FormGroup = this.fb.group({
    newsletter: [true],           // Zgoda na e-mail
    smsNotifications: [false],    // Zgoda na SMS
    searchEngineIndexing: [true], // Czy profil ma być w Google
    personalizedAds: [true],      // Zgoda na dopasowanie reklam
    thirdPartyData: [false]       // Zgoda na przekazywanie danych partnerom
  });

  onSubmit() {
    this.loading.set(true);
    console.log('Zapisywanie ustawień prywatności:', this.form.value);

    setTimeout(() => {
      this.loading.set(false);
    }, 800);
  }

  onDownloadData() {
    console.log('Rozpoczęto generowanie paczki danych (RODO)');
    alert('Twoja prośba została przyjęta. Link do pobrania danych wyślemy na e-mail.');
  }
}
