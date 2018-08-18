import {NgModule} from '@angular/core';
import {PreloadAllModules, RouterModule, Routes} from '@angular/router';
import {HomeComponent} from './home/home.component';
import {ListComponent} from './list/list.component';
import {TrueListComponent} from './list/true-list/true-list.component';
import {ExploreComponent} from './list/explore/explore.component';
import {LocationComponent} from "./location/location.component";

const routes: Routes = [
  {path: '', redirectTo: '/home', pathMatch: 'full'},
  {path: 'home', component: HomeComponent},
  {path: 'list', component: ListComponent},
  {path: 'true-list', component: TrueListComponent},
  {path: 'location', component: LocationComponent},
  {path: 'explore', component: ExploreComponent},
  {path: 'undefined', redirectTo: ''}
];

@NgModule({
  imports: [RouterModule.forRoot(routes)],
  exports: [RouterModule]
})
export class AppRoutingModule {
}

