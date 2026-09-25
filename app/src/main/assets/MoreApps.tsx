import React, { useState, useEffect } from 'react';
import { MY_APPS, AppInfo, CURRENT_APP_ID, getOtherApps } from '../config/apps';

interface MoreAppsProps {
  isOpen: boolean;
  onClose: () => Unit | void;
  currentAppId?: string;
}

export const MoreAppsModal: React.FC<MoreAppsProps> = ({
  isOpen,
  onClose,
  currentAppId = CURRENT_APP_ID
}) => {
  const [dontShowToday, setDontShowToday] = useState(false);
  const otherApps = getOtherApps(currentAppId);

  if (!isOpen) return null;

  const handleClose = () => {
    if (dontShowToday) {
      const today = new Date().toISOString().split('T')[0];
      localStorage.setItem('moreAppsDismissed', today);
    }
    onClose();
  };

  const openWeb = (url: string) => {
    window.open(url, '_blank');
  };

  const openAppOrStore = (packageName: string) => {
    // Open Play Store or market
    const playStoreUrl = `https://play.google.com/store/apps/details?id=${packageName}`;
    window.open(playStoreUrl, '_blank');
  };

  return (
    <div style={{
      position: 'fixed',
      top: 0,
      left: 0,
      right: 0,
      bottom: 0,
      backgroundColor: 'rgba(0, 0, 0, 0.85)',
      display: 'flex',
      alignItems: 'center',
      justifyContent: 'center',
      zIndex: 9999,
      padding: '16px'
    }}>
      <div style={{
        background: '#16181C',
        border: '1px solid #2F3336',
        borderRadius: '20px',
        maxWidth: '440px',
        width: '100%',
        padding: '24px',
        color: '#FFFFFF',
        boxShadow: '0 20px 40px rgba(0,0,0,0.8)'
      }}>
        {/* Header */}
        <div style={{ display: 'flex', justifyContent: 'space-between', alignItems: 'center', marginBottom: '8px' }}>
          <h2 style={{ margin: 0, fontSize: '20px', fontWeight: 800 }}>🔥 App Kami Lainnya</h2>
          <button
            onClick={handleClose}
            style={{
              background: 'transparent',
              border: 'none',
              color: '#71767B',
              fontSize: '22px',
              cursor: 'pointer',
              padding: '4px'
            }}
          >
            ✕
          </button>
        </div>
        <p style={{ margin: '0 0 20px 0', fontSize: '13px', color: '#71767B' }}>
          Download video dari platform favoritmu dengan kualitas tertinggi, 100% Gratis!
        </p>

        {/* 3 App Cards */}
        <div style={{ display: 'flex', flexDirection: 'column', gap: '12px' }}>
          {otherApps.map((app) => (
            <div
              key={app.id}
              style={{
                display: 'flex',
                alignItems: 'center',
                justifyContent: 'space-between',
                padding: '14px',
                background: '#0F1115',
                border: '1px solid #2F3336',
                borderRadius: '14px'
              }}
            >
              <div style={{ display: 'flex', alignItems: 'center', gap: '12px' }}>
                <div style={{
                  width: '42px',
                  height: '42px',
                  borderRadius: '10px',
                  background: app.color,
                  display: 'flex',
                  alignItems: 'center',
                  justifyContent: 'center',
                  fontWeight: 900,
                  fontSize: '16px',
                  color: '#FFFFFF'
                }}>
                  {app.name.charAt(0)}
                </div>
                <div>
                  <div style={{ display: 'flex', alignItems: 'center', gap: '6px' }}>
                    <span style={{ fontWeight: 700, fontSize: '15px' }}>{app.name}</span>
                    <span style={{
                      fontSize: '10px',
                      background: '#00BA7C',
                      color: '#000000',
                      fontWeight: 800,
                      padding: '2px 6px',
                      borderRadius: '4px'
                    }}>
                      Gratis
                    </span>
                  </div>
                  <div style={{ fontSize: '12px', color: '#71767B' }}>{app.desc}</div>
                </div>
              </div>

              <div style={{ display: 'flex', gap: '6px' }}>
                <button
                  onClick={() => openWeb(app.web)}
                  style={{
                    background: '#1D9BF0',
                    border: 'none',
                    color: '#FFF',
                    padding: '8px 12px',
                    borderRadius: '8px',
                    fontSize: '12px',
                    fontWeight: 700,
                    cursor: 'pointer'
                  }}
                >
                  Buka Web
                </button>
                <button
                  onClick={() => openAppOrStore(app.android)}
                  style={{
                    background: 'transparent',
                    border: '1px solid #2F3336',
                    color: '#FFF',
                    padding: '8px 10px',
                    borderRadius: '8px',
                    fontSize: '12px',
                    fontWeight: 600,
                    cursor: 'pointer'
                  }}
                >
                  Download APK
                </button>
              </div>
            </div>
          ))}
        </div>

        {/* Footer Checkbox */}
        <div style={{ display: 'flex', alignItems: 'center', marginTop: '18px', gap: '8px' }}>
          <input
            type="checkbox"
            id="dontShowToday"
            checked={dontShowToday}
            onChange={(e) => setDontShowToday(e.target.checked)}
            style={{ width: '16px', height: '16px', cursor: 'pointer' }}
          />
          <label htmlFor="dontShowToday" style={{ fontSize: '12px', color: '#71767B', cursor: 'pointer' }}>
            Jangan tampilkan lagi hari ini
          </label>
        </div>
      </div>
    </div>
  );
};

export const MoreAppsFooterSection: React.FC<{ currentAppId?: string }> = ({
  currentAppId = CURRENT_APP_ID
}) => {
  const otherApps = getOtherApps(currentAppId);

  return (
    <div style={{
      padding: '16px',
      background: '#16181C',
      borderTop: '1px solid #2F3336',
      textAlign: 'center',
      marginTop: '20px'
    }}>
      <div style={{ fontSize: '13px', fontWeight: 700, color: '#E7E9EA', marginBottom: '12px' }}>
        🔥 More Apps From Us
      </div>
      <div style={{ display: 'flex', justifyContent: 'center', gap: '20px' }}>
        {otherApps.map((app) => (
          <a
            key={app.id}
            href={app.web}
            target="_blank"
            rel="noopener noreferrer"
            style={{
              textDecoration: 'none',
              display: 'flex',
              flexDirection: 'column',
              alignItems: 'center',
              gap: '6px'
            }}
          >
            <div style={{
              width: '36px',
              height: '36px',
              borderRadius: '8px',
              background: app.color,
              display: 'flex',
              alignItems: 'center',
              justifyContent: 'center',
              color: '#FFF',
              fontWeight: 800,
              fontSize: '14px'
            }}>
              {app.name.charAt(0)}
            </div>
            <span style={{ fontSize: '11px', color: '#71767B', fontWeight: 600 }}>{app.name}</span>
          </a>
        ))}
      </div>
    </div>
  );
};
