import React, { useState, useEffect } from 'react';
import { Modal } from '../../components/ui/Modal';
import { Button } from '../../components/ui/Button';
import { Input } from '../../components/ui/Input';
import { Badge } from '../../components/ui/Badge';
import { useAuth } from '../../context/AuthContext';
import api from '../../api/client';
import { User, Phone, MapPin, Lock, Save, ShieldCheck } from 'lucide-react';

interface UserProfileModalProps {
  isOpen: boolean;
  onClose: () => void;
}

export const UserProfileModal: React.FC<UserProfileModalProps> = ({ isOpen, onClose }) => {
  const { user, updateUser } = useAuth();
  const [fullName, setFullName] = useState('');
  const [phone, setPhone] = useState('');
  const [address, setAddress] = useState('');
  const [currentPassword, setCurrentPassword] = useState('');
  const [newPassword, setNewPassword] = useState('');
  const [isLoading, setIsLoading] = useState(false);
  const [isSaving, setIsSaving] = useState(false);

  useEffect(() => {
    if (isOpen) {
      fetchProfile();
      setCurrentPassword('');
      setNewPassword('');
    }
  }, [isOpen]);

  const fetchProfile = async () => {
    setIsLoading(true);
    try {
      const res = await api.get('/auth/profile');
      const data = res.data.data;
      setFullName(data.fullName || user?.fullName || '');
      setPhone(data.phone || '');
      setAddress(data.address || '');
    } catch (err) {
      setFullName(user?.fullName || '');
    } finally {
      setIsLoading(false);
    }
  };

  const handleSaveProfile = async (e: React.FormEvent) => {
    e.preventDefault();
    if (newPassword.trim() && !currentPassword.trim()) {
      alert('Current password is required to change password.');
      return;
    }

    setIsSaving(true);
    try {
      const res = await api.put('/auth/profile', {
        fullName,
        phone,
        address,
        currentPassword: currentPassword.trim() || undefined,
        newPassword: newPassword.trim() || undefined,
      });

      const updatedData = res.data.data;
      if (updatedData?.fullName) {
        updateUser({ fullName: updatedData.fullName });
      }

      alert('Profile updated successfully!');
      setCurrentPassword('');
      setNewPassword('');
      onClose();
    } catch (err: any) {
      alert(err.response?.data?.message || 'Failed to update profile.');
    } finally {
      setIsSaving(false);
    }
  };

  return (
    <Modal isOpen={isOpen} onClose={onClose} title="👤 Account Profile Settings" maxWidth="lg">
      <form onSubmit={handleSaveProfile} className="space-y-5 font-sans">
        {/* User Info Header Badge */}
        <div className="bg-slate-900 text-white p-4 rounded-xl flex items-center justify-between">
          <div className="flex items-center space-x-3">
            <div className="w-10 h-10 rounded-full bg-blue-600 text-white flex items-center justify-center font-bold text-base">
              {fullName ? fullName.charAt(0).toUpperCase() : 'U'}
            </div>
            <div>
              <h3 className="font-bold text-sm text-white">{fullName || 'User Profile'}</h3>
              <p className="text-xs text-slate-400">{user?.email}</p>
            </div>
          </div>
          <Badge variant={user?.role === 'ADMIN' ? 'red' : 'blue'}>
            {user?.role || 'USER'}
          </Badge>
        </div>

        {/* Input Form Fields */}
        <div className="space-y-4 text-xs">
          <div>
            <label className="block font-semibold text-slate-700 mb-1">Full Name</label>
            <Input
              value={fullName}
              onChange={(e) => setFullName(e.target.value)}
              placeholder="Your full name"
              required
            />
          </div>

          <div className="grid grid-cols-1 sm:grid-cols-2 gap-3">
            <div>
              <label className="block font-semibold text-slate-700 mb-1">Phone Number</label>
              <Input
                value={phone}
                onChange={(e) => setPhone(e.target.value)}
                placeholder="+1 800 555 0199"
              />
            </div>
            <div>
              <label className="block font-semibold text-slate-700 mb-1">Role / Authority</label>
              <Input value={user?.role || 'USER'} disabled className="bg-slate-100 text-slate-500 font-mono" />
            </div>
          </div>

          <div>
            <label className="block font-semibold text-slate-700 mb-1">Address / Location</label>
            <Input
              value={address}
              onChange={(e) => setAddress(e.target.value)}
              placeholder="742 Evergreen Terrace, Springfield"
            />
          </div>

          <div className="pt-2 border-t border-slate-100 space-y-3">
            <label className="block font-semibold text-slate-700 mb-1">
              Change Password <span className="text-slate-400 font-normal">(Leave blank to keep current password)</span>
            </label>
            <Input
              type="password"
              value={currentPassword}
              onChange={(e) => setCurrentPassword(e.target.value)}
              placeholder="Current password (required if setting new password)"
            />
            <Input
              type="password"
              value={newPassword}
              onChange={(e) => setNewPassword(e.target.value)}
              placeholder="Enter new password (min. 6 characters)"
            />
          </div>
        </div>

        {/* Actions */}
        <div className="flex justify-end space-x-3 pt-3 border-t border-slate-100">
          <Button variant="outline" size="sm" type="button" onClick={onClose}>
            Cancel
          </Button>
          <Button variant="primary" size="sm" type="submit" isLoading={isSaving} icon={<Save className="w-4 h-4" />}>
            Save Profile Changes
          </Button>
        </div>
      </form>
    </Modal>
  );
};
