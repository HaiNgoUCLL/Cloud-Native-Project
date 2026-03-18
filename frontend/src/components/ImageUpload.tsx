'use client';

import { useState, useRef } from 'react';
import { Upload, X } from 'lucide-react';
import toast from 'react-hot-toast';
import api from '@/lib/axios';

interface ImageUploadProps {
  currentUrl: string;
  onUpload: (url: string) => void;
}

export function ImageUpload({ currentUrl, onUpload }: ImageUploadProps) {
  const [uploading, setUploading] = useState(false);
  const [preview, setPreview] = useState(currentUrl);
  const fileRef = useRef<HTMLInputElement>(null);

  const handleFile = async (file: File) => {
    if (file.size > 5 * 1024 * 1024) {
      toast.error('File size must be under 5MB');
      return;
    }

    if (!['image/jpeg', 'image/png', 'image/webp', 'image/gif'].includes(file.type)) {
      toast.error('Only JPEG, PNG, WebP, and GIF allowed');
      return;
    }

    setPreview(URL.createObjectURL(file));
    setUploading(true);

    try {
      const formData = new FormData();
      formData.append('file', file);
      const res = await api.post('/api/files/upload', formData, {
        headers: { 'Content-Type': 'multipart/form-data' },
      });
      const url = res.data.data;
      onUpload(url);
      toast.success('Image uploaded!');
    } catch {
      toast.error('Failed to upload image');
      setPreview(currentUrl);
    } finally {
      setUploading(false);
    }
  };

  const handleDrop = (e: React.DragEvent) => {
    e.preventDefault();
    const file = e.dataTransfer.files[0];
    if (file) handleFile(file);
  };

  const handleChange = (e: React.ChangeEvent<HTMLInputElement>) => {
    const file = e.target.files?.[0];
    if (file) handleFile(file);
  };

  return (
    <div>
      <div
        onClick={() => fileRef.current?.click()}
        onDrop={handleDrop}
        onDragOver={(e) => e.preventDefault()}
        style={{
          border: '2px dashed var(--border2)',
          borderRadius: '12px',
          padding: preview ? '0' : '20px',
          textAlign: 'center',
          cursor: 'pointer',
          background: 'var(--bg3)',
          position: 'relative',
          overflow: 'hidden',
          minHeight: '100px',
        }}
      >
        {preview ? (
          <>
            <img
              src={preview.startsWith('blob:') || preview.startsWith('http') ? preview : `http://localhost:8080${preview}`}
              alt="Preview"
              style={{ width: '100%', height: '120px', objectFit: 'cover', borderRadius: '10px' }}
            />
            <button
              type="button"
              onClick={(e) => {
                e.stopPropagation();
                setPreview('');
                onUpload('');
              }}
              style={{
                position: 'absolute',
                top: '6px',
                right: '6px',
                width: '24px',
                height: '24px',
                borderRadius: '50%',
                background: 'rgba(0,0,0,0.6)',
                color: '#fff',
                border: 'none',
                cursor: 'pointer',
                display: 'flex',
                alignItems: 'center',
                justifyContent: 'center',
              }}
            >
              <X size={14} />
            </button>
          </>
        ) : (
          <div>
            <Upload size={24} color="var(--text3)" style={{ margin: '0 auto 8px' }} />
            <p style={{ fontSize: '12px', fontWeight: 500, color: 'var(--text3)' }}>
              {uploading ? 'Uploading...' : 'Click or drag image here'}
            </p>
          </div>
        )}
      </div>
      <input ref={fileRef} type="file" accept="image/*" onChange={handleChange} style={{ display: 'none' }} />
    </div>
  );
}
