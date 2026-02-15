import {Component, inject, Input, OnChanges, OnInit, SimpleChanges} from '@angular/core';
import {ICategory} from '../../core/models/i-category';
import {CategoryService} from '../../core/service/category-service';
import {RouterLink} from '@angular/router';

@Component({
  selector: 'app-breadcrumbs',
  imports: [
    RouterLink
  ],
  templateUrl: './breadcrumbs.html',
  styleUrl: './breadcrumbs.css',
})
export class Breadcrumbs implements OnChanges {
  private categoryService = inject(CategoryService);

  @Input() categoryId?: number;
  path: ICategory[] = [];

  ngOnChanges(changes: SimpleChanges): void {
    if (changes['categoryId'] && this.categoryId) {
      this.categoryService.getCategoryPath(this.categoryId).subscribe(data => {
        this.path = data;
      });
    }
  }
}
