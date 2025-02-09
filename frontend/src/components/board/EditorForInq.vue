<template>
    <div>
        <tiptap-vuetify
            v-model="content"
            :extensions="extensions"
            :toolbar-attributes="{ color: '' }"
            placeholder="Write something …"
            />
        <div class="button_box">
        <v-container fluid>
            <v-combobox
            v-model="tags"
            :filter="filter"
            :hide-no-data="!search"
            :items="items"
            :search-input.sync="search"
            hide-selected
            label="Search for an option"
            small-chips
            solo
            >
            <template v-slot:no-data>
                <v-list-item>
                <span class="subheading">Create</span>
                <v-chip
                    :color="`${colors[nonce - 1]} lighten-3`"
                    label
                    small
                >
                    {{ search }}
                </v-chip>
                </v-list-item>
            </template>
            <template v-slot:selection="{ attrs, item, parent, selected }">
                <v-chip
                v-if="item === Object(item)"
                v-bind="attrs"
                :color="`${item.color} lighten-3`"
                :input-value="selected"
                label
                small
                >
                <span class="pr-2">
                    {{ item.text }}
                </span>
                <v-icon
                    small
                    @click="parent.selectItem(item)"
                >
                    $delete
                </v-icon>
                </v-chip>
            </template>
            <template v-slot:item="{ index, item }">
                <v-text-field
                v-if="editing === item"
                v-model="editing.text"
                autofocus
                flat
                background-color="transparent"
                hide-details
                solo
                @keyup.enter="edit(index, item)"
                ></v-text-field>
                <v-chip
                v-else
                :color="`${item.color} lighten-3`"
                dark
                label
                small
                >
                {{ item.text }}
                </v-chip>
                <v-spacer></v-spacer>
                <v-list-item-action @click.stop>
                <v-btn
                    icon
                    @click.stop.prevent="edit(index, item)"
                >
                    <v-icon>{{ editing !== item ? 'mdi-pencil' : 'mdi-check' }}</v-icon>
                </v-btn>
                </v-list-item-action>
            </template>
            </v-combobox>
        </v-container>
            
        <div class="button_box2">
          <div class="checkBox">
            <v-checkbox v-if="isAdmin()" v-model="notice" :label="`공지사항`" value ></v-checkbox>
          </div>
          <v-btn @click="snackbar = true">
              취소
          </v-btn>
          <!-- 게시물 삭제 클릭시 알림창 -->
          <div class="text-center">            
              <v-snackbar v-model="snackbar"
              :timeout="-1" centered height="60px">
              게시물 작성을 취소하시겠습니까?
                  <template v-slot:action="{ attrs }">
                      <div @click="$router.go(-1)">
                        <v-btn color="white" text v-bind="attrs">
                        확인</v-btn>
                      </div>                  
                      <v-btn color="red" text v-bind="attrs" @click="snackbar = false">
                      취소</v-btn>
                  </template>
              </v-snackbar>
          </div>
          <v-btn color="light-blue lighten-1 text center" @click="contentSubmit()" class="regist_btn">
              등록
          </v-btn>
          </div>
        </div>
    </div>
</template>
 
<script>

import { TiptapVuetify, History, Blockquote, Link, Underline, Strike, Italic, ListItem,
           Bold, Code, HorizontalRule, HardBreak, Image, Heading } from 'tiptap-vuetify'

export default {
    components: { TiptapVuetify },
    props: {
        board: {
            type: Object,
            required: true
        }
    },
    created() {
        this.tags = JSON.parse(this.board.tags)
        this.content = this.board.content
        this.notice = this.board.notice
        console.log(this.notice)

        if (this.notice == 'true') {
          this.notice = Boolean(true)
        } else {
          this.notice = Boolean(false)
        }

        console.log(this.notice)
    },
    data: () => ({
      content: '',
      notice: false,
      activator: null,
      attach: null,
      colors: ['green', 'purple', 'indigo', 'cyan', 'teal', 'orange'],
      editing: null,
      editingIndex: -1,
      items: [
        { header: '문의하실 카테고리를 선택해주세요' },
        {
          text: '사이트 이용 문의',
          color: 'blue',
        },
        {
          text: '결제 문의',
          color: 'red',
        },
        {
          text: '수강 문의',
          color: 'green',
        },
        {
          text: '회원가입/탈퇴 문의',
          color: 'purple',
        },
        {
          text: '이벤트 문의',
          color: 'indigo',
        },
        {
          text: '기타 문의',
          color: 'cyan',
        }
      ],
      nonce: 1,
      menu: false,
      tags: [
        {
          text: '사이트 이용 문의',
          color: 'blue',
        },
      ],
      x: 0,
      search: null,
      y: 0,

      extensions: [
        History,
        Blockquote,
        Link,
        Underline,
        Strike,
        Italic,
        ListItem,
        Bold,
        Code,
        HorizontalRule,
        HardBreak,
        Image,
        [Heading, {
            options: {
            levels: [1, 2, 3]
            }
        }],
      ]
    }),

    watch: {
      tags (val, prev) {
        if (val.length === prev.length) return

        this.tags = val.map(v => {
          if (typeof v === 'string') {
            v = {
              text: v,
              color: this.colors[this.nonce - 1],
            }

            this.items.push(v)

            this.nonce++
          }

          return v
        })
      },
    },

    methods: {
      edit (index, item) {
        if (!this.editing) {
          this.editing = item
          this.editingIndex = index
        } else {
          this.editing = null
          this.editingIndex = -1
        }
      },
      filter (item, queryText, itemText) {
        if (item.header) return false

        const hasValue = val => val != null ? val : ''

        const text = hasValue(itemText)
        const query = hasValue(queryText)

        return text.toString()
          .toLowerCase()
          .indexOf(query.toString().toLowerCase()) > -1
      },
      contentSubmit() {
        this.tags = JSON.stringify(this.tags)
        
        if (this.tags.length > 400) {
          alert('태그가 너무 많습니다.')
        } else {
          const { content, tags, notice } = this
          this.$emit("fromEditor", { content, tags, notice })
          console.log('editor 단계의 notice 값 : ' + this.notice)
        }
      },
      isAdmin() {
        return this.$cookies.get('ROLES').includes("ROLE_ADMIN")
      }
    },
  }
</script>

<style scoped>
.tiptap-vuetify-editor .ProseMirror {
    min-height: 600px !important;
}
.v-application .mr-9 {
     min-height: 600px !important;
}
.v-application .ProseMirror {
    min-height: 600px !important;
}
.ProseMirror {
 min-height: 600px !important;
}
.container.container--fluid {
  padding: 4px 0px 0px 0px
}
.button_box2 {
    display: flex;
    justify-content: flex-end;
    margin: 0px;
}
.regist_btn {
  margin-left: 10px;
}
.v-input--selection-controls.v-input {
  margin: 5px 15px;
}
</style>