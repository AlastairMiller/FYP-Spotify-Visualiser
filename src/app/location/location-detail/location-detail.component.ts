import {Component, OnInit} from '@angular/core';
import {RefinedPlaylistService} from '../../core/services';
import {RefinedPlaylist, RefinedTrack} from '../../core/models';
import {ActivatedRoute} from "@angular/router";


@Component({
  selector: 'app-list-detail-page',
  templateUrl: './location-detail.component.html'
})
export class LocationDetailComponent implements OnInit{

  private currentCountry: string;
  private currentCountryPlaylists: RefinedPlaylist[];
  private mostPopularSongsHere: RefinedTrack[];
  private mostPopularGenresHere: string[];

  constructor(private playlistService: RefinedPlaylistService,
              private route: ActivatedRoute) {
  }

  ngOnInit(): void {
    this.currentCountry = this.route.snapshot.paramMap.get('countryName');
  }


}
