import {Pipe, PipeTransform} from '@angular/core';
import {Countries} from '../models/countries.model';

@Pipe({
  name: 'codeToCountry'
})
export class CountryCodeToNamePipe implements PipeTransform {
  kvObjectArray = Countries.asObject;
  // @ts-ignore
  countryMap = new Map(this.kvObjectArray.map(i => [i.Code, i.Name]));


  transform(countryCode: string) {
    return this.countryMap.get(countryCode);
  }
}


