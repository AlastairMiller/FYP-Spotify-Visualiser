import {NgModule} from '@angular/core';
import {LocationComponent} from './location.component';
import {LeafletModule} from '@asymmetrik/ngx-leaflet';

@NgModule({
  imports: [ LeafletModule.forRoot()],
  declarations: [LocationComponent]
})
export class LocationModule {}
