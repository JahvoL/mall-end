package com.example.controller;

import cn.hutool.core.util.StrUtil;
import com.auth0.jwt.JWT;
import com.example.common.Result;
import com.example.service.UserService;
import com.example.entity.Address;
import com.example.service.AddressService;
import com.example.entity.User;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.toolkit.Wrappers;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import org.springframework.web.bind.annotation.*;

import javax.annotation.Resource;
import javax.servlet.http.HttpServletRequest;
import java.util.List;

// 表明该类是一个控制器，其中的方法都会返回HTTP响应体，并且类会作为Bean由Spring管理。
@RestController
@RequestMapping("/api/address")
public class AddressController {

    // 资源注入
    // @Resource：这是Java EE的注解，用于依赖注入。Spring会自动在其Bean容器中寻找并注入相对应的实例。
    @Resource
    private AddressService addressService;
    @Resource
    private HttpServletRequest request;
    @Resource
    private UserService userService;

    // 获取用户方法：此方法从HTTP请求的头部信息token中解析JWT令牌，提取用户名，并通过userService获取对应的用户对象。
    // JWT.decode(token).getAudience().get(0)：从令牌中解码并获取第一个观众（这里指用户的用户名）。
    public User getUser() {
        String token = request.getHeader("token");
        String username = JWT.decode(token).getAudience().get(0);
        return userService.getOne(Wrappers.<User>lambdaQuery().eq(User::getUsername, username));
    }

    // 用户身份验证和信息获取
    // @PostMapping：处理POST请求，用于新增地址。
    // @RequestBody Address address：将请求体中的JSON数据自动解析成Address对象。
    // 保存操作：调用addressService的save方法保存地址。
    // 保存从前端传输过来的地址到数据库，并返回一个Result信息。
    @PostMapping
    public Result<?> save(@RequestBody Address address) {
        addressService.save(address);
        return Result.success();
    }

    // @PutMapping：处理PUT请求，用于更新地址。
    // 更新操作：调用addressService的updateById方法更新地址。
    @PutMapping
    public Result<?> update(@RequestBody Address address) {
        addressService.updateById(address);
        return Result.success();
    }

    // @DeleteMapping("/{id}")：处理DELETE请求，用于根据ID删除地址。
    // @PathVariable Long id：从URL路径中提取地址ID。
    @DeleteMapping("/{id}")
    public Result<?> delete(@PathVariable Long id) {
        addressService.removeById(id);
        return Result.success();
    }

    // @GetMapping("/{id}")：处理GET请求，用于根据ID获取地址详情。
    @GetMapping("/{id}")
    public Result<?> findById(@PathVariable Long id) {
        return Result.success(addressService.getById(id));
    }

    // @GetMapping：处理GET请求，用于获取所有地址信息，没有指定路径，因此使用的是类级别的@RequestMapping定义的路径。
    // 查询逻辑：使用Wrappers.lambdaQuery()创建查询条件，按地址ID降序排序，并且筛选当前用户的地址（getUser().getId()获取当前用户ID）。
    // 返回结果：调用addressService.list(query)获取地址列表并返回。
    @GetMapping
    public Result<?> findAll() {
        LambdaQueryWrapper<Address> query = Wrappers.<Address>lambdaQuery().orderByDesc(Address::getId);
        query.eq(Address::getUserId, getUser().getId());
        List<Address> list = addressService.list(query);
        return Result.success(list);
    }

    @GetMapping("/page")
    public Result<?> findPage(
            @RequestParam(required = false, defaultValue = "1") Integer pageNum,
            @RequestParam(required = false, defaultValue = "10") Integer pageSize) {
        LambdaQueryWrapper<Address> query = Wrappers.<Address>lambdaQuery().orderByDesc(Address::getId);
//        query.eq(Address::getUserId, getUser().getId());
        IPage<Address> page = addressService.page(new Page<>(pageNum, pageSize), query);
        return Result.success(page);
    }

    @GetMapping("/page/front")
    public Result<?> Front(
            @RequestParam(required = false, defaultValue = "1") Integer pageNum,
            @RequestParam(required = false, defaultValue = "10") Integer pageSize) {
        User user = getUser();
        if(user == null) {
            return Result.success(new Page<>());
        }
        LambdaQueryWrapper<Address> query = Wrappers.<Address>lambdaQuery().orderByDesc(Address::getId);
        query.eq(Address::getUserId, getUser().getId());
        IPage<Address> page = addressService.page(new Page<>(pageNum, pageSize), query);
        return Result.success(page);
    }

}
