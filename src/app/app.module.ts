import {BrowserModule} from '@angular/platform-browser';
import {NgModule} from '@angular/core';

import {AppComponent} from './app.component';
import {HomeModule} from './home/home.module';
import {CoreModule} from './core';
import {AppRoutingModule} from './app-routing.module';
import {SharedModule} from './shared/shared.module';
import {FormsModule} from '@angular/forms';
import {FooterComponent, HeaderComponent} from './shared/layout';
import {LoggerService} from './core/services/logger.service';
import {ConsoleLoggerService} from './core/services/console-logger.service';
import {ListModule} from "./list/list.module";
import {LeafletModule} from "@asymmetrik/ngx-leaflet";
import {LocationModule} from "./location/location.module";
import {BrowserAnimationsModule} from "@angular/platform-browser/animations";

@NgModule({
  declarations: [
    AppComponent, HeaderComponent, FooterComponent
  ],
  imports: [
    BrowserModule,
    BrowserAnimationsModule,
    CoreModule,
    FormsModule,
    HomeModule,
    ListModule,
    LocationModule,
    SharedModule,
    AppRoutingModule,
    LeafletModule.forRoot()
  ],
  providers: [{ provide: LoggerService, useClass: ConsoleLoggerService },],
  bootstrap: [AppComponent]
})
export class AppModule {
}
