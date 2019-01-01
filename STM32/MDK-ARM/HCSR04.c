//#include "HCSR04.h"
// 
//#define HCSR04_PORT     GPIOB
//#define HCSR04_CLK      RCC_APB2Periph_GPIOB
//#define HCSR04_TRIG     GPIO_Pin_5
//#define HCSR04_ECHO     GPIO_Pin_6

//#define TRIG_Send  PBout(5) 
//#define ECHO_Reci  PBin(6)

//uint16_t msHcCount = 0;//ms??

//void Hcsr04Init()
//{  
//    TIM_TimeBaseInitTypeDef  TIM_TimeBaseStructure;     //?????????????
//    GPIO_InitTypeDef GPIO_InitStructure;
//    RCC_APB2PeriphClockCmd(HCSR04_CLK, ENABLE);
//     
//        //IO???
//    GPIO_InitStructure.GPIO_Pin =HCSR04_TRIG;       //??????
//    GPIO_InitStructure.GPIO_Speed = GPIO_Speed_50MHz;
//    GPIO_InitStructure.GPIO_Mode = GPIO_Mode_Out_PP;//????
//    GPIO_Init(HCSR04_PORT, &GPIO_InitStructure);
//    GPIO_ResetBits(HCSR04_PORT,HCSR04_TRIG);
//     
//    GPIO_InitStructure.GPIO_Pin =   HCSR04_ECHO;     //??????
//    GPIO_InitStructure.GPIO_Mode = GPIO_Mode_IN_FLOATING;//????
//    GPIO_Init(HCSR04_PORT, &GPIO_InitStructure);  
//        GPIO_ResetBits(HCSR04_PORT,HCSR04_ECHO);    
//     
//            //?????? ???????TIM6
//        RCC_APB1PeriphClockCmd(RCC_APB1Periph_TIM6, ENABLE);   //????RCC??
//        //??????????
//        TIM_DeInit(TIM2);
//        TIM_TimeBaseStructure.TIM_Period = (1000-1); //???????????????????????????         ???1000?1ms
//        TIM_TimeBaseStructure.TIM_Prescaler =(72-1); //??????TIMx???????????  1M????? 1US??
//        TIM_TimeBaseStructure.TIM_ClockDivision=TIM_CKD_DIV1;//???
//        TIM_TimeBaseStructure.TIM_CounterMode = TIM_CounterMode_Up;  //TIM??????
//        TIM_TimeBaseInit(TIM6, &TIM_TimeBaseStructure); //??TIM_TimeBaseInitStruct?????????TIMx???????         
//        
//        TIM_ClearFlag(TIM6, TIM_FLAG_Update);   //??????,?????????????
//        TIM_ITConfig(TIM6,TIM_IT_Update,ENABLE);    //?????????
//        hcsr04_NVIC();
//    TIM_Cmd(TIM6,DISABLE);     
//}


////tips:static?????????????????,????????????
//static void OpenTimerForHc()        //?????
//{
//        TIM_SetCounter(TIM6,0);//????
//        msHcCount = 0;
//        TIM_Cmd(TIM6, ENABLE);  //??TIMx??
//}
// 
//static void CloseTimerForHc()        //?????
//{
//        TIM_Cmd(TIM6, DISABLE);  //??TIMx??
//}
// 
// 
// //NVIC??
//void hcsr04_NVIC()
//{
//            NVIC_InitTypeDef NVIC_InitStructure;
//            NVIC_PriorityGroupConfig(NVIC_PriorityGroup_2);
//    
//            NVIC_InitStructure.NVIC_IRQChannel = TIM6_IRQn;             //????1??
//            NVIC_InitStructure.NVIC_IRQChannelPreemptionPriority = 0;  //???????????1
//            NVIC_InitStructure.NVIC_IRQChannelSubPriority = 0;         //???????????1
//            NVIC_InitStructure.NVIC_IRQChannelCmd = ENABLE;        //????
//            NVIC_Init(&NVIC_InitStructure);
//}


////???6??????
//void TIM6_IRQHandler(void)   //TIM3??
//{
//        if (TIM_GetITStatus(TIM6, TIM_IT_Update) != RESET)  //??TIM3????????
//        {
//                TIM_ClearITPendingBit(TIM6, TIM_IT_Update  );  //??TIMx?????? 
//                msHcCount++;
//        }
//}
// 

////???????
//u32 GetEchoTimer(void)
//{
//        u32 t = 0;
//        t = msHcCount*1000;//??MS
//        t += TIM_GetCounter(TIM6);//??US
//          TIM6->CNT = 0;  //?TIM2???????????
//                Delay_Ms(50);
//        return t;
//}
// 

////??????????? ??????????????,??????
////?????????,????????????????
//float Hcsr04GetLength(void )
//{
//        u32 t = 0;
//        int i = 0;
//        float lengthTemp = 0;
//        float sum = 0;
//        while(i!=5)
//        {
//        TRIG_Send = 1;      //????????
//        Delay_Us(20);
//        TRIG_Send = 0;
//        while(ECHO_Reci == 0);      //??????????
//            OpenTimerForHc();        //?????
//            i = i + 1;
//            while(ECHO_Reci == 1);
//            CloseTimerForHc();        //?????
//            t = GetEchoTimer();        //????,????1US
//            lengthTemp = ((float)t/58.0);//cm
//            sum = lengthTemp + sum ;
//        
//    }
//        lengthTemp = sum/5.0;
//        return lengthTemp;
//}


///*:::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::
//** ????: Delay_Ms_Ms
//** ????: ??1MS (?????????????)            
//** ????:time (ms) ??time<65535
//:::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::*/
//void Delay_Ms(uint16_t time)  //????
//{ 
//    uint16_t i,j;
//    for(i=0;i<time;i++)
//          for(j=0;j<10260;j++);
//}
///*:::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::
//** ????: Delay_Ms_Us
//** ????: ??1us (?????????????)
//** ????:time (us) ??time<65535                 
//:::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::*/
//void Delay_Us(uint16_t time)  //????
//{ 
//    uint16_t i,j;
//    for(i=0;i<time;i++)
//          for(j=0;j<9;j++);
//}