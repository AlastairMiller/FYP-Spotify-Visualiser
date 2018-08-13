import {NgModule} from '@angular/core';
import {CommonModule} from '@angular/common';

import {
  RefinedAlbumService, RefinedArtistService, RefinedPlaylistService
} from './services';
import {LoggerService} from "./services/logger.service";
import {RefinedTrackService} from "./services/refined-track.service";
import {RefinedTrack} from "./models";
import {CountryCodeToNamePipe} from "./pipes/country-code-to-name.pipe";

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
    CountryCodeToNamePipe
  ],
  exports: [CountryCodeToNamePipe],
  declarations: [CountryCodeToNamePipe]
})
export class CoreModule {
}
