import { WebPlugin } from '@capacitor/core';
import html2canvas from 'html2canvas';

import type { ScreenshotPlugin } from './definitions';

export class ScreenshotWeb extends WebPlugin implements ScreenshotPlugin {
  async take( options: { id?: string } = {} ): Promise<{ base64: string }> {
    return await new Promise((ok, nook) => {
      const { id } = options
      const element = id ? document.getElementById(id) : document.getElementsByTagName('ion-app')[0] 

      if (!element) { nook(new Error('ELement not found')); }

      html2canvas(element as HTMLElement).then((ret: HTMLCanvasElement) => {
        ok({ base64: ret.toDataURL().split(',')[1] });
      }, (err: Error) => {
        nook(err);
      });
    });
  }
}
