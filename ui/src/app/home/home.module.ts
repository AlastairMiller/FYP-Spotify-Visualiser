import {NgModule} from '@angular/core';
import {HomeComponent} from './home.component';
import {BrowserModule} from '@angular/platform-browser';
import {SafePipe} from './safe-pipe.pipe';
import {CommonModule} from '@angular/common';
import {CoreModule} from '../core';
import {AgWordCloudModule} from 'angular4-word-cloud';
import {BrowserAnimationsModule} from '@angular/platform-browser/animations';


@NgModule({
  imports: [
    BrowserModule,
    BrowserAnimationsModule,
    CoreModule,
    AgWordCloudModule.forRoot()
  ],
  declarations: [
    HomeComponent,
    SafePipe
  ],
  providers: [SafePipe]
})
export class HomeModule {
}
