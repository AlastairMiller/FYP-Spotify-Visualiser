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

@NgModule({
  declarations: [
    AppComponent, HeaderComponent, FooterComponent
  ],
  imports: [
    BrowserModule,
    CoreModule,
    FormsModule,
    HomeModule,
    SharedModule,
    AppRoutingModule
  ],
  providers: [{ provide: LoggerService, useClass: ConsoleLoggerService },],
  bootstrap: [AppComponent]
})
export class AppModule {
}
