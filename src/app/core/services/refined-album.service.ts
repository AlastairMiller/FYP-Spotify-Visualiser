import {Injectable} from '@angular/core';
import {catchError, map, tap} from 'rxjs/operators';
import {RefinedAlbum} from '../models';
import {Observable} from 'rxjs/internal/Observable';
import {LoggerService} from './logger.service';
import {environment} from '../../../environments/environment';
import {HttpClient} from '@angular/common/http';

@Injectable({ providedIn: 'root' })
export class RefinedAlbumService {

  constructor( private  http: HttpClient,
               private logger: LoggerService) {
  }

  private url = environment.api_url + '/albums';

  getById(id: string): Observable<RefinedAlbum> {
    return this.http.get<RefinedAlbum>(this.url + '/' + id )
      .pipe(tap(refinedAlbum  => this.logger.info(`fetched album id=${refinedAlbum.id}`)),
      catchError(this.logger.error(`failed to get album from id`)));
}

}
