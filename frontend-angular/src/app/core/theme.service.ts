import { Injectable, effect, signal } from '@angular/core';

const THEME_KEY = 'ihub_theme';

@Injectable({ providedIn: 'root' })
export class ThemeService {
  readonly dark = signal(localStorage.getItem(THEME_KEY) === 'dark');

  constructor() {
    effect(() => {
      const isDark = this.dark();
      document.documentElement.classList.toggle('ihub-dark-theme', isDark);
      localStorage.setItem(THEME_KEY, isDark ? 'dark' : 'light');
    });
  }

  toggle(): void { this.dark.update(value => !value); }
}
