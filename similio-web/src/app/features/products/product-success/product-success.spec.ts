import { ComponentFixture, TestBed } from '@angular/core/testing';

import { ProductSuccess } from './product-success';

describe('ProductSuccess', () => {
  let component: ProductSuccess;
  let fixture: ComponentFixture<ProductSuccess>;

  beforeEach(async () => {
    await TestBed.configureTestingModule({
      imports: [ProductSuccess]
    })
    .compileComponents();

    fixture = TestBed.createComponent(ProductSuccess);
    component = fixture.componentInstance;
    fixture.detectChanges();
  });

  it('should create', () => {
    expect(component).toBeTruthy();
  });
});
