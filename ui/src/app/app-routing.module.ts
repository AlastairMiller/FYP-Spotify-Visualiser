import {NgModule} from '@angular/core';
import {PreloadAllModules, RouterModule, Routes} from '@angular/router';
import {HomeComponent} from './home/home.component';
import {ListComponent} from './list/list.component';
import {TrueListComponent} from './list/true-list/true-list.component';
import {SearchComponent} from './list/search/search.component';
import {LocationComponent} from "./location/location.component";
import {LocationDetailComponent} from "./location/location-detail/location-detail.component";

const routes: Routes = [
  {path: '', redirectTo: '/home', pathMatch: 'full'},
  {path: 'home', component: HomeComponent},
  {path: 'list', component: ListComponent},
  {path: 'true-list', component: TrueListComponent},
  {path: 'location', component: LocationComponent},
  {path: 'location/:locationName', component: LocationDetailComponent},
  {path: 'search', component: SearchComponent},
  {path: 'undefined', redirectTo: ''}
];

@NgModule({
  imports: [RouterModule.forRoot(routes)],
  exports: [RouterModule]
})
export class AppRoutingModule {
}

