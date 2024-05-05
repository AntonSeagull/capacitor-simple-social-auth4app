import { ListenerCallback, WebPlugin } from '@capacitor/core';
import type { SimpleSocialAuth4AppPlugin, SocialAuthEnum } from './definitions';

export class SimpleSocialAuth4AppWeb
  extends WebPlugin
  implements SimpleSocialAuth4AppPlugin
{
  private sessionKey: string = '';

  async auth(options: {
    social: SocialAuthEnum;
  }): Promise<{ key: string; success: boolean }> {
    this.sessionKey = this.generateRandomKey(256);
    const authUrl = `https://auth4app.com/auth?soc=${options.social}&key=${this.sessionKey}`;

    // Открываем новое окно для аутентификации
    const win = window.open(authUrl, '_blank');
    return new Promise((resolve, reject) => {
      const interval = setInterval(() => {
        if (win && win.closed) {
          clearInterval(interval);
          this.fetchAPIResponse(this.sessionKey)
            .then(() => {
              resolve({ key: this.sessionKey, success: true });
            })
            .catch(() => {
              reject({ key: this.sessionKey, success: false });
            });
        }
      }, 500);
    });
  }

  addListener(eventName: string, listenerFunc: ListenerCallback): any {
    const handle = super.addListener(eventName, listenerFunc);
    return Promise.resolve(handle);
  }

  private async fetchAPIResponse(key: string): Promise<any> {
    try {
      const response = await fetch(`https://api.auth4app.com/hash?key=${key}`);
      if (!response.ok) {
        throw new Error(`HTTP error! status: ${response.status}`);
      }
      const json = await response.json();
      if (json.type === 'success') {
        this.notifyListeners('authSuccess', { key, userInfo: json.data });
      } else {
        this.notifyListeners('authError', { key, error: json.data });
        throw new Error('Authentication failed');
      }
    } catch (error) {
      this.notifyListeners('authError', {
        key,
        error: 'Network or server error',
      });
      throw error;
    }
  }

  private generateRandomKey(length: number): string {
    const characters =
      'ABCDEFGHIJKLMNOPQRSTUVWXYZabcdefghijklmnopqrstuvwxyz0123456789';
    let result = '';
    for (let i = 0; i < length; i++) {
      result += characters.charAt(
        Math.floor(Math.random() * characters.length),
      );
    }
    return result;
  }
}
