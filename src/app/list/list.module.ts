import {NgModule} from '@angular/core';
import {ListComponent} from './list.component';
import {TrueListComponent} from './true-list/true-list.component';
import {SearchComponent} from './search/search.component';
import {ListRoutingModule} from './list-routing.module';
import {BrowserModule} from '@angular/platform-browser';
import {CoreModule} from '../core';
import {LeafletModule} from '@asymmetrik/ngx-leaflet';
import {BsDropdownModule} from 'ngx-bootstrap';


@NgModule({
    imports: [
      ListRoutingModule,
      BrowserModule,
      BsDropdownModule.forRoot(),
      CoreModule,
      LeafletModule.forRoot()
    ],
    declarations: [
      ListComponent,
      TrueListComponent,
      SearchComponent
    ]
  }
)
export class ListModule {
}

