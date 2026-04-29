import { request } from './request'

export const mapApi = {
  geocode(address) {
    return request('/map/geocode', {
      method: 'POST',
      data: { address }
    })
  },
  search(address) {
    return request('/map/search', {
      method: 'POST',
      data: { address }
    })
  }
}
