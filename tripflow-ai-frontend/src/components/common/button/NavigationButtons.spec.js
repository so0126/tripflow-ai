import { describe, it, expect } from 'vitest'
import { mount } from '@vue/test-utils'
import NavigationButtons from '@/components/common/button/NavigationButtons.vue'

// 툴체인 스모크 테스트: SFC 컴파일 + '@' 별칭 + jsdom + test-utils가
// 한 번에 동작하는지 검증한다. (실제 자식 컴포넌트까지 마운트)
describe('NavigationButtons', () => {
  it('backText와 next 슬롯 내용을 렌더한다', () => {
    const wrapper = mount(NavigationButtons, {
      props: { backText: '뒤로' },
      slots: { 'next-content': '다음으로' },
    })
    expect(wrapper.text()).toContain('뒤로')
    expect(wrapper.text()).toContain('다음으로')
  })

  it('isNextDisabled=true면 다음 버튼이 disabled된다', () => {
    const wrapper = mount(NavigationButtons, {
      props: { isNextDisabled: true },
    })
    const buttons = wrapper.findAll('button')
    const nextBtn = buttons[buttons.length - 1] // FilledButton = 다음
    expect(nextBtn.attributes('disabled')).toBeDefined()
  })

  it('버튼 클릭 시 back/next 이벤트를 emit한다', async () => {
    const wrapper = mount(NavigationButtons)
    const buttons = wrapper.findAll('button')
    await buttons[0].trigger('click')              // OutlineButton = 뒤로
    await buttons[buttons.length - 1].trigger('click') // FilledButton = 다음
    expect(wrapper.emitted('back')).toHaveLength(1)
    expect(wrapper.emitted('next')).toHaveLength(1)
  })
})
