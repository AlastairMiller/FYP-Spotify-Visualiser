import {Injectable} from '@angular/core';
import {HttpClient} from '@angular/common/http';
import {LoggerService} from './logger.service';
import {environment} from '../../../environments/environment';
import {Observable} from 'rxjs/internal/Observable';
import {RefinedAlbum, RefinedArtist, RefinedTrack} from '../models';
import {catchError, tap} from 'rxjs/operators';

@Injectable({providedIn: 'root'})
export class RefinedArtistService {
  constructor(private http: HttpClient,
              private logger: LoggerService) {
  }

  private url = environment.api_url + '/artists';

  getByIds(ids: string[]): Observable<RefinedArtist[]> {
    const request = this.url + '/' + ids.toString().replace(new RegExp(',', 'g'), '&') + '/multi';
    return this.http.get<RefinedArtist[]>(request)
      .pipe(
        tap(refinedArtist => this.logger.info(`fetched Artists with id=${refinedArtist.toString()}`)),
        catchError(this.logger.error(`failed to get Artist from id`))
      );
  }

  getMultiple(ids: string): Observable<RefinedArtist[]> {
    return this.http.get<RefinedArtist[]>(this.url + '/' + ids + '/multi');
  }


  getMostPopular(offset: number, limit: number): Observable<RefinedArtist[]> {
      return this.http.get<RefinedArtist[]>(this.url + '/most-popular?offset=' + offset + '&limit=' + limit)
        .pipe(tap(() => this.logger.info('fetched most popular Artists')));
  }

}

