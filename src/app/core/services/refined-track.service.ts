import {Injectable} from '@angular/core';
import {catchError, map, tap} from 'rxjs/operators';
import {RefinedAlbum, RefinedPlaylist, RefinedTrack} from '../models';
import {HttpClient} from '@angular/common/http';
import {LoggerService} from './logger.service';
import {environment} from '../../../environments/environment';
import {Observable} from 'rxjs/internal/Observable';


@Injectable({providedIn: 'root'})
export class RefinedTrackService {
  constructor(private  http: HttpClient,
              private logger: LoggerService) {
  }

  private url = environment.api_url + '/tracks';

  getRandom(): Observable<RefinedTrack> {
    return this.http.get<RefinedTrack>(this.url + '/random')
      .pipe(tap(refinedTrack => this.logger.info(`fetched track id=${refinedTrack.id}`)));
  }

  getMultiple(ids: string): Observable<RefinedTrack[]> {
    return this.http.get<RefinedTrack[]>(this.url + '/' + ids + '/multi');
  }

  search(searchTerm: string, offset: number, limit: number): Observable<RefinedTrack[]> {
    return this.http.get<RefinedTrack[]>(this.url + '/name=' + searchTerm + '?offset=' + offset + '&limit=' + limit)
      .pipe(tap(() => this.logger.info('searched for tracks based on term: ' + searchTerm)));
  }


  getMostPopularCountry(trackId: string): Observable<String> {
    return this.http.get<string>(this.url + '/' + trackId + '/popular-country');
  }

  getMostPopular(offset: number, limit: number): Observable<RefinedTrack[]> {
    return this.http.get<RefinedTrack[]>(this.url + '/most-popular?offset=' + offset + '&limit=' + limit)
      .pipe(tap(() => this.logger.info('fetched most popular Tracks')));

  }
}
