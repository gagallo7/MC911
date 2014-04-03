@.formatting.string = private constant [4 x i8] c"%d\0A\00"
define i32 @main() {
entry:
  %tmp0 = alloca i32
  store i32 0, i32 * %tmp0
  %tmp1 = add i32 1, 2
  %tmp2 = getelementptr [4 x i8] * @.formatting.string, i32 0, i32 0
  %tmp4 = call i32 (i8 *, ...)* @printf(i8 * %tmp2, i32 %tmp1)
  %tmp5 = load i32 * %tmp0
  ret i32 %tmp5
}
declare i32 @printf (i8 *, ...)
