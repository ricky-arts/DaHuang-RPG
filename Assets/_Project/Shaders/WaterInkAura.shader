Shader "HongHuang/Oriental/WaterInkAura"
{
    Properties
    {
        _MainTex ("Base Texture", 2D) = "white" {}
        _InkColor ("Ink Tint (墨韵)", Color) = (0.1, 0.12, 0.15, 1.0)
        _MineralColor ("Mineral Gold/Cyan (青绿丹砂)", Color) = (0.18, 0.55, 0.48, 1.0)
        _AuraSpeed ("Qi Flow Speed (气劲流速)", Range(0.1, 5.0)) = 1.0
        _NoiseScale ("Noise Scale (云纹尺度)", Range(1.0, 20.0)) = 5.0
    }

    SubShader
    {
        Tags { "RenderType"="Transparent" "Queue"="Transparent" "RenderPipeline"="UniversalPipeline" }
        LOD 100
        Blend SrcAlpha OneMinusSrcAlpha
        ZWrite Off
        Cull Off

        Pass
        {
            Name "WaterInkForward"
            HLSLPROGRAM
            #pragma vertex vert
            #pragma fragment frag

            #include "Packages/com.unity.render-pipelines.universal/ShaderLibrary/Core.hlsl"

            struct Attributes
            {
                float4 positionOS   : POSITION;
                float2 uv           : TEXCOORD0;
            };

            struct Varyings
            {
                float4 positionHCS  : SV_POSITION;
                float2 uv           : TEXCOORD0;
                float3 worldPos     : TEXCOORD1;
            };

            TEXTURE2D(_MainTex);
            SAMPLER(sampler_MainTex);

            CBUFFER_START(UnityPerMaterial)
                float4 _MainTex_ST;
                float4 _InkColor;
                float4 _MineralColor;
                float _AuraSpeed;
                float _NoiseScale;
            CBUFFER_END

            Varyings vert(Attributes input)
            {
                Varyings output;
                output.positionHCS = TransformObjectToHClip(input.positionOS.xyz);
                output.uv = TRANSFORM_TEX(input.uv, _MainTex);
                output.worldPos = TransformObjectToWorld(input.positionOS.xyz);
                return output;
            }

            // 简易国风水墨流云噪声模拟
            float SimplexNoise(float2 uv)
            {
                return sin(uv.x * _NoiseScale + _Time.y * _AuraSpeed) * cos(uv.y * _NoiseScale + _Time.y * _AuraSpeed * 0.8);
            }

            half4 frag(Varyings input) : SV_Target
            {
                half4 col = SAMPLE_TEXTURE2D(_MainTex, sampler_MainTex, input.uv);
                float noise = SimplexNoise(input.uv);

                // 水墨晕染渐变：边缘留白，中心聚墨，夹带青绿金石流光
                float inkAlpha = saturate(col.a + noise * 0.25);
                half3 finalColor = lerp(_InkColor.rgb, _MineralColor.rgb, saturate(noise + 0.5));

                return half4(finalColor, inkAlpha * col.a);
            }
            ENDHLSL
        }
    }
    FallBack "Hidden/Universal Render Pipeline/FallbackError"
}
