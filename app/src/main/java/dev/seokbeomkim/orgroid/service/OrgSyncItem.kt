package dev.seokbeomkim.orgroid.service

/**
 * OrgSyncItem class represents an item to be synced.
 *
 * Google Drive, Dropbox, WebDAV could be possible for the sync target.
 * And the list of OrgSyncItem will be managed by SettingManager, and
 * used by OrgSyncService.
 */
class OrgSyncItem {
    enum class RemoteSyncTarget {
        GoogleDrive,
        DropBox,
        WebDAV
    }

    var targetFilePath: String = ""
}