import { Component, inject, OnInit } from '@angular/core';
import { Button } from '../button/button';
import { ActivatedRoute, Router } from '@angular/router';
import { FormsModule } from '@angular/forms';
import { takeUntilDestroyed } from '@angular/core/rxjs-interop';

@Component({
  selector: 'app-search-bar',
  imports: [
    Button,
    FormsModule
  ],
  templateUrl: './search-bar.html',
  styleUrl: './search-bar.css',
})
export class SearchBar {
  private readonly router = inject(Router);

  searchQuery: string = '';

  onSearch() {
    const trimmedQuery = this.searchQuery.trim();

    this.router.navigate(['/search'], {
      queryParams: {
        query: trimmedQuery || null
      },
      queryParamsHandling: 'merge'
    });
  }
}
