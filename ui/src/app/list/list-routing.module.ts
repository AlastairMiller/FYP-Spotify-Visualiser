import {RouterModule, Routes} from '@angular/router';
import {NgModule} from '@angular/core';
import {ListComponent} from './list.component';
import {TrueListComponent} from './true-list/true-list.component';

const routes: Routes = [
  {
    path: 'list',
    component: ListComponent,
    children: [
      {path: 'true-list', component: TrueListComponent}
    ]
  },
];

@NgModule({
  imports: [RouterModule.forChild(routes)],
  exports: [RouterModule]
})
export class ListRoutingModule {}
