import request from './request'

/** 分类获取社区帖子列表 */
export function getCommunityPosts(params) {
  return request({
    url: '/community/posts',
    method: 'GET',
    params,
  })
}

/** 发布社区帖子 */
export function createCommunityPost(data) {
  return request({
    url: '/community/posts',
    method: 'POST',
    data,
  })
}

/** 获取帖子详情 */
export function getCommunityPostDetail(id) {
  return request({
    url: `/community/posts/${id}`,
    method: 'GET',
  })
}

/** 关闭帖子 */
export function closeCommunityPost(id, authorId) {
  return request({
    url: `/community/posts/${id}/close`,
    method: 'POST',
    params: { authorId },
  })
}
