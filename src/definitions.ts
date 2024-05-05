export enum SocialAuthEnum {
  VK = 'vk',
  OK = 'ok',
  GOOGLE = 'google',
  YANDEX = 'yandex',
  MAILRU = 'mailru',
}

export interface SimpleSocialAuth4AppPlugin {
  auth(options: {
    social: SocialAuthEnum;
  }): Promise<{ key: string; success: boolean }>;
  addListener(
    eventName: 'authSuccess',
    listenerFunc: (event: AuthEvent) => void,
  ): Promise<PluginListenerHandle> & PluginListenerHandle;
  addListener(
    eventName: 'authError',
    listenerFunc: (event: AuthEvent) => void,
  ): Promise<PluginListenerHandle> & PluginListenerHandle;
  removeAllListeners(): Promise<void>;
}

export interface AuthEvent {
  key: string; // Ключ сессии
  userInfo?: any; // Информация о пользователе
}

export interface PluginListenerHandle {
  remove: () => Promise<void>;
}
