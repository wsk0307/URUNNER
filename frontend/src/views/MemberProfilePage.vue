<template>
    <div style="position: relative;min-height:900px;">
        <member-profile-form @submit="profileSubmit"/>
        <v-spacer></v-spacer>        
    </div>
</template>


<script>

import MemberProfileForm from '@/components/member/MemberProfileForm.vue'
import axios from 'axios'
import Vue from 'vue'

export default {
    name: 'MemberProfilePage',
    components: {
        MemberProfileForm
    },
    methods: {
        profileSubmit (payload) {            
            const { userIdInToken, nickname, password, introduce } = payload
            axios.put(`http://localhost:7777/profile/${userIdInToken}`, { userIdInToken, nickname, password, introduce })
                    .then(res => {
                        if (res.data != "") {
                            alert('프로필 변경 완료')
                            this.$store.state.profile.introduce = res.data.introduce
                            Vue.$cookies.set("NICKNAME", res.data.nickname)
                        } else {
                            alert('프로필 변경 실패 - ' + res.data)                            
                        }
                    })
                    .catch(res => {
                        alert(res.response.data.message)
                        alert('잘못된 접근! 로그인부터 해주세요')
                    })
            },
    }
}
</script>


<style scoped>
.background_image {
    background-repeat: no-repeat;
    background-position: 40% 0;
    background-size: auto;
}
.title_image {
    display: flex;
    justify-content: center;
    margin: 30px;
    margin-bottom: 0px;
    margin-top: 120px;
    padding-bottom: 20px;
    width: 50vw;
    border-bottom: 1px solid #BDBDBD;
}
.board_title {
    text-align: center;
    font-size: 32px;
    font-weight: 700;
    line-height: 32px;
    color: #333d4b;
    margin-bottom: 60px;
}
.box0 {
    display: flex;
    justify-content: center;
}
</style>