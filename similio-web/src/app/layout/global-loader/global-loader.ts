import { Component } from '@angular/core';
import {Spinner} from '../../shared/spinner/spinner';

@Component({
  selector: 'app-global-loader',
  imports: [
    Spinner
  ],
  templateUrl: './global-loader.html',
  styleUrl: './global-loader.css',
})
export class GlobalLoader {

}
