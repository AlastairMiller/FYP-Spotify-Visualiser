import {NgModule} from '@angular/core';
import {CommonModule} from '@angular/common';

import {
  RefinedAlbumService, RefinedArtistService, RefinedPlaylistService
} from './services';
import {LoggerService} from './services/logger.service';
import {RefinedTrackService} from './services';
import {RefinedTrack} from './models';
import {CountryCodeToNamePipe} from './pipes/country-code-to-name.pipe';
import {CountryNameToCodePipe} from './pipes/country-name-to-code.pipe';
import {BasicChartComponent} from './charts/basic-chart.component';

@NgModule({
  imports: [
    CommonModule
  ],
  providers: [
    RefinedTrack,
    RefinedTrackService,
    RefinedAlbumService,
    RefinedArtistService,
    RefinedPlaylistService,
    LoggerService,
    CountryCodeToNamePipe,
    CountryNameToCodePipe
  ],
  exports: [CountryCodeToNamePipe,
    CountryNameToCodePipe, BasicChartComponent],
  declarations: [CountryCodeToNamePipe, CountryNameToCodePipe, BasicChartComponent]
})
export class CoreModule {
}
