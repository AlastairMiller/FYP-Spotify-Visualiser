import {NgModule} from '@angular/core';
import {HomeComponent} from './home.component';
import {BrowserModule} from '@angular/platform-browser';
import {SafePipe} from './safe-pipe.pipe';


@NgModule({
  imports: [
    BrowserModule
  ],
  declarations: [
    HomeComponent,
    SafePipe
  ],
  providers: [SafePipe]
})
export class HomeModule {
}
