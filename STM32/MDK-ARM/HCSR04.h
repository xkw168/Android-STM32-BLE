#include "main.h"
#include "stm32f4xx_hal.h"


void Hcsr04Init();
static void OpenTimerForHc();
static void CloseTimerForHc();
void hcsr04_NVIC();
void TIM6_IRQHandler(void);
uint32_t GetEchoTimer(void);
float Hcsr04GetLength(void );
void Delay_Ms(uint16_t time); 
void Delay_Us(uint16_t time);