import {NgModule} from '@angular/core';
import {ListComponent} from './list.component';
import {TrueListComponent} from './true-list/true-list.component';
import {ExploreComponent} from './explore/explore.component';
import {ListRoutingModule} from './list-routing.module';


@NgModule({
    imports: [
      ListRoutingModule
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

