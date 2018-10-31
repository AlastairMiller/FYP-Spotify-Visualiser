import {Injectable} from '@angular/core';
import {HttpClient} from '@angular/common/http';
import {LoggerService} from './logger.service';
import {RefinedPlaylist} from '../models';
import {Observable} from 'rxjs/internal/Observable';
import {tap} from 'rxjs/operators';
import {environment} from '../../../environments/environment';

@Injectable({providedIn: 'root'})
export class RefinedPlaylistService {
  constructor(private http: HttpClient,
              private logger: LoggerService) {
  }

  private url = environment.api_url + '/playlists';

  getMostPopular(offset: number, limit: number): Observable<RefinedPlaylist[]> {
    return this.http.get<RefinedPlaylist[]>(this.url + '/most-popular?offset=' + offset + '&limit=' + limit)
      .pipe(tap(() => this.logger.info('fetched most popular Playlists')));
  }

  getPlaylistLocation(countryCode: string, offset: number, limit: number): Observable<RefinedPlaylist[]> {
    return this.http.get<RefinedPlaylist[]>(this.url + '/country-code/' + countryCode + '?offset=' + offset + '&limit=' + limit)
      .pipe(tap(() => this.logger.info('fetched playlists for ' + countryCode)));
  }

  getPopularTracks(countryCode: string, offset: number, limit: number): Observable<Map<string, number>> {
    return this.http.get <Map<string, number>>(this.url + '/popular-tracks/' + countryCode + '?offset=' + offset + '&limit=' + limit)
      .pipe(tap(() => this.logger.info('fetched most popular tracks for ' + countryCode)));
  }

  getPopularArtists(countryCode: string, offset: number, limit: number): Observable<Map<string, number>> {
    return this.http.get <Map<string, number>>(this.url + '/popular-artists/' + countryCode + '?offset=' + offset + '&limit=' + limit)
      .pipe(tap(() => this.logger.info('fetched most popular artists for ' + countryCode)));
  }

  getPopularGenres(countryCode: string, offset: number, limit: number): Observable<Map<string, number>> {
    return this.http.get <Map<string, number>>( this.url + '/popular-genres/' + countryCode + '?offset=' + offset + '&limit=' + limit)
      .pipe(tap(() => this.logger.info('fetched most popular genres for ' + countryCode)));
  }


}
