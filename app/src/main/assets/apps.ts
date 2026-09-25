export interface AppInfo {
  id: string;
  name: string;
  desc: string;
  web: string;
  android: string;
  color: string;
}

export const MY_APPS: AppInfo[] = [
  { id: 'ytsave', name: 'YTSave', desc: 'YouTube Downloader', web: 'https://ytsave.vercel.app', android: 'com.ytsave.downloader', color: '#FF0000' },
  { id: 'savetok', name: 'SaveTok', desc: 'TikTok No Watermark', web: 'https://savetok.vercel.app', android: 'com.savetok.downloader', color: '#FE2C55' },
  { id: 'reelssave', name: 'ReelsSave', desc: 'IG Reels & Story', web: 'https://reelssave.vercel.app', android: 'com.reelssave.downloader', color: '#DD2A7B' },
  { id: 'xsave', name: 'XSave', desc: 'X Video Downloader', web: 'https://xsave.vercel.app', android: 'com.xsave.downloader', color: '#1DA1F2' },
];

export const CURRENT_APP_ID = 'xsave';

export function getOtherApps(currentAppId: string = CURRENT_APP_ID): AppInfo[] {
  return MY_APPS.filter(a => a.id !== currentAppId);
}
