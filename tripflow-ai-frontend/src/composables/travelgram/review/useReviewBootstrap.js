import { ref } from 'vue'

export function useReviewBootstrap({ reviewStore, api, planId, planTitle }) {
  const currentPlanInfo = ref(null)
  const isReady = ref(false)

  const fetchPlanDetail = async () => {
    const res = await api.getPlanDetail(planId)
    const data = res.data.data
    const rawBudget = Number(data.plan.budget || 0)

    currentPlanInfo.value = {
      location:
        data.days?.[0]?.places?.[0]?.address?.split(' ').slice(0, 2).join(' ') || 'Seoul',
      date: `${data.plan.startDate} ~ ${data.plan.endDate}`,
      rawCost: rawBudget,
      cost: rawBudget.toLocaleString(),
      itinerary: data.days.map((day) => ({
        dayNumber: day.day.dayIndex,
        title: day.day.title,
        date: day.day.planDate,
        places: day.places.map((place) => ({
          title: place.placeName,
          startAt: place.startAt,
          details: {
            type: place.placeType || 'ETC',
            desc: place.description,
            gallery: place.firstImage2 ? [place.firstImage2] : [],
            address: place.address,
          },
        })),
      })),
    }
  }

  const createReviewSession = async () => {
    reviewStore.setplanInfo(planId, planTitle)
    await fetchPlanDetail()

    const res = await api.createReview(planId)
    reviewStore.setReviewInfo(res.data.data.reviewPostId)
    isReady.value = true
  }

  return {
    currentPlanInfo,
    isReady,
    fetchPlanDetail,
    createReviewSession,
  }
}
