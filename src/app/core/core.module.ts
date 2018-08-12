import {NgModule} from '@angular/core';
import {CommonModule} from '@angular/common';

import {
  RefinedAlbumService, RefinedArtistService, RefinedPlaylistService
} from './services';
import {LoggerService} from "./services/logger.service";
import {RefinedTrackService} from "./services/refined-track.service";
import {RefinedTrack} from "./models";

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
    LoggerService
  ],
  declarations: []
})
export class CoreModule { }
