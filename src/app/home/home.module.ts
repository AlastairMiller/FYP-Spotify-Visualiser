import {NgModule} from '@angular/core';
import {HomeComponent} from './home.component';
import {BrowserModule} from '@angular/platform-browser';
import {SafePipe} from './safe-pipe.pipe';
import {CommonModule} from '@angular/common';
import {CoreModule} from '../core';
import {CountryCodeToNamePipe} from '../core/pipes/country-code-to-name.pipe';


@NgModule({
  imports: [
    BrowserModule,
    CoreModule
  ],
  declarations: [
    HomeComponent,
    SafePipe
  ],
  providers: [SafePipe]
})
export class HomeModule {
}
