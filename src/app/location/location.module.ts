import {NgModule} from '@angular/core';
import {LocationComponent} from './location.component';
import {LeafletModule} from '@asymmetrik/ngx-leaflet';
import {LocationDetailComponent} from './location-detail/location-detail.component';
import {BrowserModule} from '@angular/platform-browser';
import {CoreModule} from '../core';

@NgModule({
  imports: [BrowserModule,
    CoreModule, LeafletModule.forRoot()],
  declarations: [LocationComponent,
    LocationDetailComponent]
})
export class LocationModule {
}
