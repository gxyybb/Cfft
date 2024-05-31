package com.example.cfft.util;


import com.baidu.tts.client.SpeechSynthesizer;
import com.baidu.tts.client.TtsMode;

public interface IOfflineResourceConst {

    TtsMode DEFAULT_SDK_TTS_MODE = TtsMode.OFFLINE;

    String VOICE_FEMALE = "F";

    String VOICE_MALE = "M";

    String VOICE_DUYY = "Y";

    String VOICE_DUXY = "X";

    String TEXT_MODEL = "bd_etts_common_text_txt_all_mand_eng_middle_big_v4.1.0_20230423.dat";

    String VOICE_MALE_MODEL = "bd_etts_common_speech_duxiaoyu_mand_eng_high_am-style24k_v4.6.0_20210721_20220822104311.dat";

    String VOICE_FEMALE_MODEL = "bd_etts_navi_speech_f7_mand_eng_high_am-style24k_v4.6.0_20210721.dat";

    String VOICE_DUXY_MODEL = "bd_etts_common_speech_duxiaoyao_mand_eng_high_am-style24k_v4.6.0_20210721_20220822104311.dat";

    String VOICE_DUYY_MODEL = "bd_etts_common_speech_duxiaoyao_mand_eng_high_am-style24k_v4.6.0_20210721_20220822104311.dat";

    String PARAM_SN_NAME = SpeechSynthesizer.PARAM_AUTH_SN;
}
