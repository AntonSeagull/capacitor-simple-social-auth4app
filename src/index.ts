import { registerPlugin } from '@capacitor/core';

import type { SimpleSocialAuth4AppPlugin } from './definitions';

const SimpleSocialAuth4App = registerPlugin<SimpleSocialAuth4AppPlugin>(
  'SimpleSocialAuth4App',
  {
    web: () => import('./web').then(m => new m.SimpleSocialAuth4AppWeb()),
  },
);

export * from './definitions';
export { SimpleSocialAuth4App };
