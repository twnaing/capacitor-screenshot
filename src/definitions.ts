export interface ScreenshotPlugin {
  take(options: { id?: string }): Promise<{ base64: string }>;
}
