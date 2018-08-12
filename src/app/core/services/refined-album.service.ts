import {Injectable} from '@angular/core';
import {catchError, map, tap} from 'rxjs/operators';
import {RefinedAlbum} from '../models';
import {Observable} from 'rxjs/internal/Observable';
import {LoggerService} from './logger.service';
import {environment} from '../../../environments/environment';
import {HttpClient} from '@angular/common/http';

@Injectable({providedIn: 'root'})
export class RefinedAlbumService {

  constructor(private  http: HttpClient,
              private logger: LoggerService) {
  }

  private url = environment.api_url + '/albums';

  getById(id: string): Observable<RefinedAlbum> {
    return this.http.get<RefinedAlbum>(this.url + '/' + id)
      .pipe(tap(refinedAlbum => this.logger.info(`fetched album id=${refinedAlbum.id}`)),
        catchError(this.logger.error(`failed to get album from id`)));
  }

  getByIds(ids: string[]): Observable<RefinedAlbum[]> {
    return this.http.get<RefinedAlbum[]>(this.url + '/' + ids.toString().replace(new RegExp(',', 'g'), '&') + '/multi')
      .pipe(
        tap(refinedArtist => this.logger.info(`fetched Artists with id=${refinedArtist.toString()}`))
      );
  }

  getMostPopular(offset: number, limit: number): Observable<RefinedAlbum[]> {
    return this.http.get<RefinedAlbum[]>(this.url + '/most-popular?offset=' + offset + '&limit=' + limit)
      .pipe(tap(() => this.logger.info('fetched most popular albums')));


  }

}
