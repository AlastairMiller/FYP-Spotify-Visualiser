import {NgModule} from '@angular/core';
import {ListComponent} from './list.component';
import {TrueListComponent} from './true-list/true-list.component';
import {ExploreComponent} from './explore/explore.component';
import {ListRoutingModule} from './list-routing.module';
import {BrowserModule} from '@angular/platform-browser';


@NgModule({
    imports: [
      ListRoutingModule,
      BrowserModule
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

