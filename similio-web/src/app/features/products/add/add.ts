import {Component, inject, OnInit, signal} from '@angular/core';
import {FormBuilder, FormControl, ReactiveFormsModule, Validators} from '@angular/forms';
import {FormSection} from '../../../shared/form-section/form-section';
import {InputField} from '../../../shared/input-field/input-field';
import {ImageUploader} from '../components/image-uploader/image-uploader';
import {CategorySelect} from '../components/category-select/category-select.component';
import {CategoryService} from '../../../core/service/category-service';
import {ICategory} from '../../../core/models/i-category';
import {Button} from '../../../shared/button/button';
import {ProductService} from '../../../core/service/product.service';
import {Select, SelectOption} from '../../../shared/select/select';
import {ActivatedRoute, Router} from '@angular/router';
import {finalize} from 'rxjs';
import {CONDITION_OPTIONS} from '../../../core/models/product/condition';
import {IImage} from '../../../core/models/product/i-image';

@Component({
  selector: 'app-add',
  imports: [
    ReactiveFormsModule,
    FormSection,
    InputField,
    ImageUploader,
    CategorySelect,
    Button,
    Select
  ],
  templateUrl: './add.html',
  styleUrl: './add.css',
})
export class Add implements OnInit {
  conditionOptions = CONDITION_OPTIONS;
  brandOptions: SelectOption[] = [
    { label: '4F', value: '4F' },
    { label: 'Adidas', value: 'ADIDAS' },
    { label: 'Asos', value: 'ASOS' },
    { label: 'Atmosphere', value: 'ATMOSPHERE' },
    { label: 'Bershka', value: 'BERSHKA' },
    { label: 'Bimba y Lola', value: 'BIMBA_Y_LOLA' },
    { label: 'C&A', value: 'CA' },
    { label: 'Calvin Klein', value: 'CALVIN_KLEIN' },
    { label: 'Champion', value: 'CHAMPION' },
    { label: 'Converse', value: 'CONVERSE' },
    { label: 'Cropp', value: 'CROPP' },
    { label: 'Desigual', value: 'DESIGUAL' },
    { label: 'Diesel', value: 'DIESEL' },
    { label: 'Dr. Martens', value: 'DR_MARTENS' },
    { label: 'Fila', value: 'FILA' },
    { label: 'Gap', value: 'GAP' },
    { label: 'Gino Rossi', value: 'GINO_ROSSI' },
    { label: 'Gucci', value: 'GUCCI' },
    { label: 'Guess', value: 'GUESS' },
    { label: 'H&M', value: 'HM' },
    { label: 'House', value: 'HOUSE' },
    { label: 'Hugo Boss', value: 'HUGO_BOSS' },
    { label: 'Jack & Jones', value: 'JACK_JONES' },
    { label: 'Jordan', value: 'JORDAN' },
    { label: 'Karl Lagerfeld', value: 'KARL_LAGERFELD' },
    { label: 'Lacoste', value: 'LACOSTE' },
    { label: 'Lee', value: 'LEE' },
    { label: 'Levi\'s', value: 'LEVIS' },
    { label: 'Louis Vuitton', value: 'LOUIS_VUITTON' },
    { label: 'Mango', value: 'MANGO' },
    { label: 'Massimo Dutti', value: 'MASSIMO_DUTTI' },
    { label: 'Michael Kors', value: 'MICHAEL_KORS' },
    { label: 'Mohito', value: 'MOHITO' },
    { label: 'Monnari', value: 'MONNARI' },
    { label: 'Moschino', value: 'MOSCHINO' },
    { label: 'New Balance', value: 'NEW_BALANCE' },
    { label: 'New Look', value: 'NEW_LOOK' },
    { label: 'Next', value: 'NEXT' },
    { label: 'Nike', value: 'NIKE' },
    { label: 'North Face', value: 'THE_NORTH_FACE' },
    { label: 'Only', value: 'ONLY' },
    { label: 'Patagonia', value: 'PATAGONIA' },
    { label: 'Pepco', value: 'PEPCO' },
    { label: 'Primark', value: 'PRIMARK' },
    { label: 'Pull & Bear', value: 'PULL_BEAR' },
    { label: 'Puma', value: 'PUMA' },
    { label: 'Ralph Lauren', value: 'RALPH_LAUREN' },
    { label: 'Reebok', value: 'REEBOK' },
    { label: 'Reserved', value: 'RESERVED' },
    { label: 'River Island', value: 'RIVER_ISLAND' },
    { label: 'Shein', value: 'SHEIN' },
    { label: 'Sinsay', value: 'SINSAY' },
    { label: 'Stradivarius', value: 'STRADIVARIUS' },
    { label: 'Supreme', value: 'SUPREME' },
    { label: 'Tally Weijl', value: 'TALLY_WEIJL' },
    { label: 'Timberland', value: 'TIMBERLAND' },
    { label: 'Tommy Hilfiger', value: 'TOMMY_HILFIGER' },
    { label: 'Under Armour', value: 'UNDER_ARMOUR' },
    { label: 'Uniqlo', value: 'UNIQLO' },
    { label: 'Vans', value: 'VANS' },
    { label: 'Vero Moda', value: 'VERO_MODA' },
    { label: 'Versace', value: 'VERSACE' },
    { label: 'Victoria\'s Secret', value: 'VICTORIAS_SECRET' },
    { label: 'Wrangler', value: 'WRANGLER' },
    { label: 'Zara', value: 'ZARA' },
    { label: 'Inna marka', value: 'OTHER', description: 'Wybierz tę opcję, jeśli Twojej marki nie ma na liście powyżej.' }
  ];

  colorOptions: SelectOption[] = [
    { label: 'Czarny', value: 'BLACK', color: '#000000' },
    { label: 'Biały', value: 'WHITE', color: '#ffffff' },
    { label: 'Szary', value: 'GRAY', color: '#808080' },
    { label: 'Beżowy', value: 'BEIGE', color: '#f5f5dc' },
    { label: 'Brązowy', value: 'BROWN', color: '#8b4513' },
    { label: 'Niebieski', value: 'BLUE', color: '#0000ff' },
    { label: 'Granatowy', value: 'NAVY', color: '#000080' },
    { label: 'Błękitny', value: 'LIGHT_BLUE', color: '#add8e6' },
    { label: 'Czerwony', value: 'RED', color: '#ff0000' },
    { label: 'Bordowy', value: 'BURGUNDY', color: '#800000' },
    { label: 'Różowy', value: 'PINK', color: '#ffc0cb' },
    { label: 'Fuksja', value: 'FUCHSIA', color: '#ff00ff' },
    { label: 'Fioletowy', value: 'PURPLE', color: '#800080' },
    { label: 'Zielony', value: 'GREEN', color: '#008000' },
    { label: 'Oliwkowy', value: 'OLIVE', color: '#808000' },
    { label: 'Żółty', value: 'YELLOW', color: '#ffff00' },
    { label: 'Pomarańczowy', value: 'ORANGE', color: '#ffa500' },
    { label: 'Złoty', value: 'GOLD', color: '#ffd700' },
    { label: 'Srebrny', value: 'SILVER', color: '#c0c0c0' },
    { label: 'Wielokolorowy', value: 'MULTICOLOR', color: 'linear-gradient(45deg, red, blue, green, yellow)' }
  ];

  sizeOptions: SelectOption[] = [
    { label: 'XXS', value: 'XXS' }, { label: 'XS', value: 'XS' }, { label: 'S', value: 'S' },
    { label: 'M', value: 'M' }, { label: 'L', value: 'L' }, { label: 'XL', value: 'XL' },
    { label: 'XXL', value: 'XXL' }, { label: '3XL', value: '3XL' }, { label: '4XL', value: '4XL' },
    { label: '32', value: '32' }, { label: '34', value: '34' }, { label: '36', value: '36' },
    { label: '38', value: '38' }, { label: '40', value: '40' }, { label: '42', value: '42' },
    { label: '44', value: '44' }, { label: '46', value: '46' }, { label: '48', value: '48' },
    { label: '50', value: '50' },
    { label: 'W28', value: 'W28' }, { label: 'W29', value: 'W29' }, { label: 'W30', value: 'W30' },
    { label: 'W31', value: 'W31' }, { label: 'W32', value: 'W32' }, { label: 'W33', value: 'W33' },
    { label: 'W34', value: 'W34' },
    { label: '37', value: '37_SHOES' }, { label: '38', value: '38_SHOES' }, { label: '39', value: '39_SHOES' },
    { label: '40', value: '40_SHOES' }, { label: '41', value: '41_SHOES' }, { label: '42', value: '42_SHOES' },
    { label: '43', value: '43_SHOES' }, { label: '44', value: '44_SHOES' }, { label: '45', value: '45_SHOES' },
    { label: 'Uniwersalny', value: 'ONE_SIZE', description: 'Rozmiar pasujący na każdą sylwetkę' },
    { label: 'Inny', value: 'OTHER' }
  ];

  private fb = inject(FormBuilder);
  private categoryService = inject(CategoryService);
  private productService = inject(ProductService);
  private router = inject(Router);
  private route = inject(ActivatedRoute);

  loading = signal(false);
  isEditMode = signal(false);
  productId: string | null = null;

  files: File[] = [];
  existingImages: IImage[] = [];
  imagesToKeep: string[] = [];

  categories: ICategory[] = [];

  form = this.fb.group({
    title: ['', [Validators.required, Validators.minLength(5)]],
    description: ['', [Validators.required, Validators.maxLength(2000)]],
    amount: [0, [Validators.required, Validators.min(1)]],
    categoryId: [0, [Validators.required]],
    brand: ['', Validators.required],
    color: ['', [Validators.required]],
    size: [''],
    condition: ['']
  });

  ngOnInit(): void {
    this.categoryService.getCategories().subscribe({
      next: (data) => {
        this.categories = data;
      },
      error: (err) => console.error('Error loading categories:', err)
    });

    const idParam = this.route.snapshot.paramMap.get('id');

    if (idParam) {
      this.productId = idParam;
      this.isEditMode.set(true);
      this.loadProductData(this.productId);
    }
  }

  private loadProductData(id: string) {
    this.loading.set(true);

    this.productService.getProductById(id).subscribe({
      next: (product) => {
        this.form.patchValue({
          title: product.title,
          description: product.description,
          amount: product.price.amount,
          categoryId: product.categoryId,
          brand: product.brand,
          color: product.color,
          size: product.size,
          condition: product.condition
        });

        if (product.images) {
          this.existingImages = product.images;
          this.imagesToKeep = product.images.map(img => img.url);
        }

        this.loading.set(false);
      },
      error: (err) => {
        console.error('Nie udało się pobrać produktu', err);
        this.loading.set(false);
        this.router.navigate(['/']);
      }
    });
  }

  getControl(name: string): FormControl {
    return this.form.get(name) as FormControl;
  }

  onFilesChanged(newFiles: File[]) {
    this.files = newFiles;
  }

  onExistingImagesChanged(keptUrls: string[]) {
    this.imagesToKeep = keptUrls;
  }

  onSubmit() {
    if (this.form.invalid) {
      this.form.markAllAsTouched();
      return;
    }

    this.loading.set(true);
    const productData = this.form.getRawValue();

    if (this.isEditMode() && this.productId) {
      this.handleUpdate(this.productId, productData);
    } else {
      this.handleCreate(productData);
    }
  }

  private handleCreate(data: any) {
    this.productService.createProduct(data, this.files)
      .pipe(
        finalize(() => this.loading.set(false))
      )
      .subscribe({
        next: (response) => {
          this.router.navigate(['/create-success'], {
            state: { product: response }
          });
        },
        error: (err) => {
          console.error('Błąd tworzenia produktu', err);
        }
      });
  }

  private handleUpdate(id: string, data: any) {
    const updateData = {
      ...data,
      remainingImages: this.imagesToKeep
    };

    this.productService.updateProduct(id, updateData, this.files)
      .pipe(
        finalize(() => this.loading.set(false))
      )
      .subscribe({
        next: () => {
          this.router.navigate(['/product-details', id])
        },
        error: (err) => {
          console.error('Błąd edycji produktu', err);
        }
      });
  }
}
