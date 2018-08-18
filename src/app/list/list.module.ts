import {NgModule} from '@angular/core';
import {ListComponent} from './list.component';
import {TrueListComponent} from './true-list/true-list.component';
import {ExploreComponent} from './explore/explore.component';
import {ListRoutingModule} from './list-routing.module';
import {BrowserModule} from '@angular/platform-browser';
import {CoreModule} from '../core';
import {LeafletModule} from '@asymmetrik/ngx-leaflet';


@NgModule({
    imports: [
      ListRoutingModule,
      BrowserModule,
      CoreModule,
      LeafletModule.forRoot()
    ],
    declarations: [
      ListComponent,
      TrueListComponent,
      ExploreComponent
    ]
  }
)
export class ListModule {
}

