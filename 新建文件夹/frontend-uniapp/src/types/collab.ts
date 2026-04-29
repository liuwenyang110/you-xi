export type CommunityFeedTab = 'nearby' | 'machinery' | 'mutualAid' | 'notice'

export type CollabSyncState = 'idle' | 'syncing' | 'success' | 'error' | 'offline'

export type CollabMessageType = 'TEXT' | 'IMAGE' | 'SYSTEM' | 'QUICK_REPLY'

export type CollabSenderRole = 'self' | 'partner' | 'system'

export type CollabSessionStatus =
  | 'PUBLISHED'
  | 'CLAIMED'
  | 'COMMUNICATING'
  | 'ARRIVING'
  | 'IN_SERVICE'
  | 'COMPLETED'
  | 'CLOSED'

export interface CommunityFeedItem {
  id: string
  backendPostId?: number
  authorId?: number
  postType: string
  source: 'backend' | 'mock' | 'system'
  title: string
  content: string
  authorName: string
  avatarText: string
  createdAt: string
  locationName: string
  distanceText: string
  areaText: string
  tagLabel: string
  statusLabel: string
  primaryActionLabel: string
  secondaryActionLabel?: string
  images: string[]
  isUrgent: boolean
  disclaimer: string
  sessionHint: string
}

export interface CollabMessage {
  id: string
  sessionId: string
  type: CollabMessageType
  sender: CollabSenderRole
  content: string
  imageUrl?: string
  createdAt: string
  pending?: boolean
}

export interface CollabSession {
  id: string
  source: 'backend' | 'mock'
  backendSessionId?: number
  backendClaimId?: number
  relatedPostId?: number
  title: string
  partnerName: string
  partnerPhoneMask?: string
  avatarText: string
  status: CollabSessionStatus
  statusLabel: string
  statusDescription: string
  lastMessagePreview: string
  updatedAt: string
  unreadCount: number
  disclaimer: string
  syncMode: 'REST_POLLING' | 'LOCAL_ONLY'
  lastAckAt?: string
  deliveryMode?: 'REST_POLLING' | 'LOCAL_ONLY'
  feedbackRating?: number
  serviceCompleted?: boolean
  localStatusOverride?: boolean
}

export interface BackendContactSession {
  id: number
  demandId?: number | null
  ownerId?: number | null
  farmerId?: number | null
  serviceItemId?: number | null
  status?: string | null
  statusLabel?: string | null
  statusDescription?: string | null
  activeFlag?: number | null
  maskedPhone?: string | null
  contactType?: string | null
  discoverySource?: string | null
  farmerFeedback?: string | null
  farmerRating?: number | null
  serviceCompleted?: number | boolean | null
  confirmedAt?: string | null
}

export interface BackendCommunityPost {
  id: number
  authorId?: number | null
  postType?: string | null
  title?: string | null
  content?: string | null
  imagesJson?: string | null
  cropCode?: string | null
  machineCategory?: string | null
  machineType?: string | null
  areaMu?: number | string | null
  locationName?: string | null
  isUrgent?: number | boolean | null
  status?: string | null
  viewCount?: number | null
  replyCount?: number | null
  createdAt?: string | null
  updatedAt?: string | null
}

export interface BackendVolunteerClaim {
  id: number
  demandId?: number | null
  postId?: number | null
  volunteerId?: number | null
  equipmentId?: number | null
  status?: string | null
  claimedAt?: string | null
  arrivedAt?: string | null
  finishedAt?: string | null
  farmerFeedback?: string | null
  farmerRating?: number | null
}

export interface CollabSyncStatusMap {
  feed: CollabSyncState
  sessions: CollabSyncState
  messages: Record<string, CollabSyncState>
  lastSyncedAt: string
}
