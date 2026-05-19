import Hotel from "@/views/planner/hotel/Recommendation.vue";
import CreatePlan from "@/views/planner/CreatePlan.vue";
import PlanList from "@/views/planner/edit/PlanList.vue";
import Payment from "@/views/planner/hotel/Payment.vue";
import BookingComplete from "@/views/planner/BookingComplete.vue";
import PlannerSummary from "@/views/planner/PlannerSummary.vue"
import PlannerTest from "@/components/planner/PlannerTest.vue"
const planner = [

  {
    path: "/planner",
    name: "plannercreate",
    component: CreatePlan,
    meta: { title: "새 플랜 생성" },
  },
  {
    path: "/planner/summary",
    name: "plansummary",
    component: PlannerSummary,
    meta: { title: "플랜 요약" },
  },
  {
    path: "/planner/edit",
    name: "planedit",
    component: PlanList,
    meta: {
      title: "플랜 편집"
    },
  },
  {
    path: "/planner/hotel",
    name: "hotel",
    component: Hotel,
    meta: { title: "호텔 추천" },
  },

  {
    path: "/planner/payment",
    name: "payment",
    component: Payment,
    meta: { title: "결제" },
  },

  {
    path: "/planner/booking-complete",
    name: "bookingComplete",
    component: BookingComplete,
    meta: { title: "예약 완료" },
  },
  {
    path: "/planner/test",
    name: "plannertest",
    component: PlannerTest,
    meta: { title: "플래너 테스트" },
  },
];

export default planner;
